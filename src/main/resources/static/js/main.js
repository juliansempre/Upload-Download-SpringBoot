// Atributos
window.styleComponentGaleria = "";

// Função para criar o HTML da galeria
var componentgaleria = (imgarquivos) => {
  return `
    <div class="p-1 md:p-2 w-full md:w-1/3">
        <img
            alt="gallery"
            class="block h-full w-full rounded-lg object-cover object-center"
            src="${imgarquivos}"/>
    </div>
  `;
};

// Função para adicionar a galeria ao elemento com classe "galeria"
var addGaleria = (styleComponentGaleria, imgarquivos) => {
  // Adicionar galeria
  var galeria = document.querySelector(".galeria");
  if (galeria) {
    galeria.innerHTML += styleComponentGaleria(imgarquivos); // Usar '+=' para adicionar cada imagem
  } else {
    console.error("Elemento com a classe 'galeria' não encontrado.");
  }
};

// Consumindo com JavaScript puro
fetch('http://localhost:8080/downloads/api')
  .then(function (res) {
    return res.json();
  })
  .then(function (data) {
    data.meusarquivos.forEach(function (arquivo) {
      var imgarquivos = 'uploads/' + arquivo;
      addGaleria(componentgaleria, imgarquivos);
    });
  })
  .catch(function (error) {
    console.log(error);
  });

//Validação

function validateForm() {
  // campos vazios
  const fileInput = document.querySelector('input[name="imagem"]');
  if (fileInput.value === '') {
    alert("O campo de arquivo está vazio. Por favor, selecione um arquivo.");
    return false; // Impedir o envio do formulário
  }
  return true; // Permitir o envio do formulário se o campo não estiver vazio
}